package omar.reachback


import grails.transaction.Transactional
import static groovyx.net.http.ContentType.TEXT
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import java.security.KeyStore
import java.util.regex.Pattern
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory


@Transactional
class SearchService {

    def grailsApplication


    def search( params ) {

        def results = []

        def source = grailsApplication.config.imagerySources[ 0 ]
        def urlParams = []
        source.requiredParams.each {
            urlParams.push( "${ it.key }=${ it.value }" )
        }
        source.urlParamMap.each {
            key, value ->
            if ( value instanceof ArrayList ) {
                urlParams.push( "${ key }=${ URLEncoder.encode( Eval.me( 'params', params, value[ 0 ] ) ) }" )
            }
            else if ( params[ key ] ) {
                urlParams.push( "${ value }=${ params[ key ] }" )
            }
        }
        def url = "${ source.url }?${ urlParams.join( "&" ) }"
        def http = new HTTPBuilder( url )


        def keyStoreConfig = grailsApplication.config.keyStores?.keyStore
        def keyStoreFile = new File( "${ keyStoreConfig?.filename }" )

        def trustStoreConfig = grailsApplication.config.keyStores?.trustStore
        def trustStoreFile = new File( "${ trustStoreConfig?.filename }" )

        if ( keyStoreFile.exists() && trustStoreFile.exists() ) {
            def keyStore = KeyStore.getInstance( KeyStore.defaultType )
            keyStoreFile.withInputStream { stream ->
                keyStore.load( stream, "${ keyStoreConfig.password }".toCharArray() )
            }

            def trustStore = KeyStore.getInstance( KeyStore.defaultType )
            trustStoreFile.withInputStream { stream ->
                trustStore.load( stream, "${ trustStoreConfig.password }".toCharArray() )
            }

            def ssl = new SSLSocketFactory( keyStore, "${ keyStoreConfig.password }", trustStore )
            http.client.connectionManager.schemeRegistry.register( new Scheme( 'https', ssl, 443 ) )
        }

        try {
            http.request( GET, TEXT ) { req ->
                response.failure = { resp, reader ->
                    println "Failure: ${ reader }"


                    return null
                }
                response.success = { resp, reader ->
                    def json = new JsonSlurper().parseText( reader.text )
                    def features = json[ source.resultsKey ]
                    features.each {
                        def map = [ : ]
                        def metadata = JsonOutput.toJson( it[ source.resultsMetadataKey ] )
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
            }
        }
        catch ( Exception event ) {
            println event


            return null
        }
    }
}
