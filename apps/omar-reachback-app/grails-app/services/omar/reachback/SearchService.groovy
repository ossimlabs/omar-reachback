package omar.reachback


import grails.transaction.Transactional
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.util.regex.Pattern



@Transactional
class SearchService {

    def grailsApplication
    def httpService


    def search( params ) {
        def results = []

        def source = grailsApplication.config.imagerySource

        def urlParams = []
        source.search.requiredParams.each {
            urlParams.push( "${ it.key }=${ it.value }" )
        }
        source.search.urlParamMap.each {
            key, value ->

            if ( value instanceof org.grails.config.NavigableMap ) {
                def urlParamsArray = []
                value.each {
                    def eval = Eval.me( 'params', params, it.value )
                    if ( eval != "" ) {
                        urlParamsArray.push( eval )
                    }
                }
                urlParams.push( "${ key }=${ URLEncoder.encode( urlParamsArray.join( " AND " ) ) }" )
            }
            else if ( params[ key ] ) {
                urlParams.push( "${ value }=${ params[ key ] }" )
            }
        }

        def url = "${ source.url }?${ urlParams.join( "&" ) }"
        println url
        def text = httpService.http( url )
        try {
            def json = new JsonSlurper().parseText( text )
            def features = json[ source.search.resultsKey ]
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


            return [ contentType: "application/json",  text: JsonOutput.toJson( results ) ]
        }
        catch ( Exception event ) {
            println event


            return null
        }
    }
}
