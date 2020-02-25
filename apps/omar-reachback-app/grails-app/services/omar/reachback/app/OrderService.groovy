package omar.reachback.app


import grails.gorm.transactions.Transactional


@Transactional
class OrderService {

    def grailsApplication
    def httpService


    def automated( sourceName ) {
        // in case a high priority ingest needs to occur
        def file = new File( "ids.txt" )
        if ( file.exists() ) {
            // pop the first id off the list
            def images = file.getText().split( "\n" ) as Collection
            def image = images[ 0 ].split( ',' )
            images.removeAt( 0 )
            file.write( images.join( "\n" ) + '\n' )

            // order it
            def params = [
                id: image[ 0 ],
                sourceName: image[ 1 ]
            ]
            order( params )
        }
        else {
            def image = Image.findByStatusLike( "PENDING" )
            if ( !image ) {
                return
            }

            def params = [
                id: image.uid,
                sourceName: sourceName
            ]
            image.status = "ORDERING"
            image.save()

            order( params )

            image.status = "ORDERED"
            image.save()
        }
    }

    def order( params ) {
        def source = grailsApplication.config.imagerySources[ "${ params.sourceName }" ].order

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
