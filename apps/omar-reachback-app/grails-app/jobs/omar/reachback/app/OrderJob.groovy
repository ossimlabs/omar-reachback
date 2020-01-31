package omar.reachback.app


import groovyx.gpars.GParsPool


class OrderJob {

    def orderService


    static triggers = {
        simple repeatInterval: ( grailsApplication.config.orderInterval ?: 1 ) * 60 * 1000
    }


    def execute() {
        if ( grailsApplication.config.orderInterval ) {
            GParsPool.withPool {
                grailsApplication.config.imagerySources.eachParallel {
                    if ( it.value.order ) {
                        orderService.automated( it.key )
                    }
                }
            }
        }
    }
}
