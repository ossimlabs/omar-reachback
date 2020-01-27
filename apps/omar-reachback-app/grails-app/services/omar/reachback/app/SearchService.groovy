package omar.reachback.app


import grails.gorm.transactions.Transactional
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import java.util.regex.Pattern



@Transactional
class SearchService {

    def grailsApplication
    def httpService


    def automated( sourceName ) {
        def endDate = new Date()
        def startDate = endDate.clone()
        startDate.setSeconds( startDate.getSeconds() - grailsApplication.config.searchInterval * 60 )
        recordResults( startDate, endDate, sourceName )
    }

    def historical( sourceName ) {
        def source = grailsApplication.config.imagerySources[ "${ sourceName }" ]

        def startDate = new SimpleDateFormat( 'yyyy-MM-dd HH:mm:ss').parse( "${ source.historicalSearch.startDate }" )
        def endDate = startDate.clone()
        def interval = source.historicalSearch.interval
        endDate.setMinutes( endDate.getMinutes() + interval )

        def currentDate = new Date()
        while ( startDate.getTime() < currentDate.getTime() && endDate.getTime() < currentDate.getTime() ) {
            recordResults( startDate, endDate, sourceName )

            startDate = new Date( endDate.getTime() )
            endDate.setMinutes( endDate.getMinutes() + interval )
        }

        endDate = currentDate
        recordResults( startDate, endDate, sourceName )
    }

    def recordResults( startDate, endDate, sourceName ) {
        def source = grailsApplication.config.imagerySources[ "${ sourceName }" ]
        def params = [
            endDate: new SimpleDateFormat( 'yyyy-MM-dd HH:mm:ss' ).format( endDate ),
            startDate: new SimpleDateFormat( 'yyyy-MM-dd HH:mm:ss' ).format( startDate ),
            sourceName: sourceName
        ]
        def results = search( params )

        results.each {
            def image = new Image(
                imageId: it.imageId,
                source: sourceName,
                uid: it.id
            )
            image.save()

            if ( image.hasErrors() ) {
                image.errors.allErrors.each { println it }
            }
        }
    }

    def search( params ) {
        def results = []
        def source = grailsApplication.config.imagerySources[ "${ params.sourceName }" ].search

        def urlParams = []
        source.requiredParams.each {
            urlParams.push( "${ it.key }=${ it.value }" )
        }
        source.urlParamMap.each {
            key, value ->

            if ( value instanceof org.grails.config.NavigableMap ) {
                def urlParamsArray = []
                value.each {
                    def eval = Eval.me( 'params', params, it.value )
                    if ( eval != '' ) {
                        urlParamsArray.push( eval )
                    }
                }
                urlParams.push( "${ key }=${ URLEncoder.encode( urlParamsArray.join( ' AND ' ) ) }" )
            }
            else if ( params[ key ] ) {
                urlParams.push( "${ value }=${ params[ key ] }" )
            }
        }

        def url = "${ source.url }?${ urlParams.join( '&' ) }"
        println 'Searching...'
        println URLDecoder.decode(url)

        def text = httpService.http( url )
        try {
            def json = new JsonSlurper().parseText( text )
            def features = json[ source.resultsKey ]
            println "Found ${ features.size() } features(s)..."

            features.each {
                def map = [ sourceName: params.sourceName ]
                def metadata = JsonOutput.toJson( it )

                source.metadataMap.each {
                    key, regexp ->
                    def pattern = Pattern.compile( regexp )
                    def value = metadata.find( pattern ) { matcher, val -> return val }
                    map.put( key, value )
                }
                results.push( map )
            }


            return results
        }
        catch ( Exception event ) {
            println event


            return null
        }
    }
}
