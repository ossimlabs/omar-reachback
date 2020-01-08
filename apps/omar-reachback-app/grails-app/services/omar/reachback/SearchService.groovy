package omar.reachback


import grails.transaction.Transactional
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.regex.Pattern



@Transactional
class SearchService {

    def grailsApplication
    def httpService


    def automated() {
        def endDate = new Date()
        def startDate = endDate.clone()
        startDate.setSeconds( startDate.getSeconds() - grailsApplication.config.searchInterval * 60 )
        recordResults( startDate, endDate )
    }

    def historical() {
        def startDate = Date.parse( 'yyyy-MM-dd HH:mm:ss', "${ grailsApplication.config.historicalSearch.startDate }" )
        def endDate = startDate.clone()
        def interval = grailsApplication.config.historicalSearch.interval
        endDate.setMinutes( endDate.getMinutes() + interval )

        def currentDate = new Date()
        while ( startDate.getTime() < currentDate.getTime() && endDate.getTime() < currentDate.getTime() ) {
            recordResults( startDate, endDate )

            startDate = new Date( endDate.getTime() )
            endDate.setMinutes( endDate.getMinutes() + interval )
        }

        endDate = currentDate
        recordResults( startDate, endDate )
    }

    def recordResults( startDate, endDate ) {
        def params = [
            endDate: endDate.format( 'yyyy-MM-dd HH:mm:ss' ),
            startDate: startDate.format( 'yyyy-MM-dd HH:mm:ss' )
        ]
        def results = search( params )

        results.each {
            def image = new Image(
                imageId: it.imageId,
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

        def source = grailsApplication.config.imagerySource.search

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
                def map = [ : ]
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
