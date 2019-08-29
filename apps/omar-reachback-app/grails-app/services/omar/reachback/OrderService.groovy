package omar.reachback


import grails.transaction.Transactional


@Transactional
class OrderService {

    def grailsApplication
    def httpService


    def order( params ) {
        def source = grailsApplication.config.imagerySource

        def urlParams = []
        source.order.requiredParams.each {
            urlParams.push( "${ it.key }=${ it.value }" )
        }
        source.order.urlParamMap.each {
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


        return [ contentType: "application/json",  text: text ]
    }
}
