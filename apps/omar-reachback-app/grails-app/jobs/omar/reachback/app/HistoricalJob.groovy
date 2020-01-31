package omar.reachback.app


import groovyx.gpars.GParsPool


class HistoricalJob {

    def searchService


    static triggers = {
        simple repeatCount: 0
    }


    def execute() {
        GParsPool.withPool {
            grailsApplication.config.imagerySources.eachParallel {
                if ( it.value.historicalSearch ) {
                    searchService.historical( it.key )
                }
            }
        }
    }
}
