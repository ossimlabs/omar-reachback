package omar.reachback


import grails.transaction.Transactional


@Transactional
class OrderService {

    def grailsApplication
    def httpService


    def automated() {
        def file = new File( "ids.txt" )

        if ( file.exists() ) {
            // pop the first id off the list
            def ids = file.getText().split( "\n" ) as Collection
            def id = ids[ 0 ]
            ids.removeAt( 0 )
            file.write( ids.join( "\n" ) + '\n' )

            // order it
            //order([ id: id ])
        }
    }

    def order( params ) {
        def source = grailsApplication.config.imagerySource.order

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
                urlParams.push( "${ key }=${ URLEncoder.encode( urlParamsArray.join( " AND " ) ) }" )
            }
            else if ( params[ key ] ) {
                urlParams.push( "${ value }=${ params[ key ] }" )
            }
        }

        def url = "${ source.url }?${ urlParams.join( "&" ) }"
        println 'Ordering...'
        println URLDecoder.decode(url)

        def text = httpService.http( url )


        return [ contentType: "application/json",  text: text ]
    }
}
