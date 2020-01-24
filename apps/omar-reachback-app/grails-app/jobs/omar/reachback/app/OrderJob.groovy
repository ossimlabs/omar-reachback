package omar.reachback.app

class OrderJob {

    def orderService


    static triggers = {
        simple repeatInterval: ( grailsApplication.config.orderInterval ?: 1 ) * 60 * 1000
    }


    def execute() {
        if ( grailsApplication.config.orderInterval ) {
            grailsApplication.config.imagerySources.each {
                if ( it.value.order ) {
                    orderService.automated( it.key )
                }
            }
        }
    }
}
