package omar.reachback.app


import groovyx.gpars.GParsPool


class SearchJob {

    def searchService


    static triggers = {
        simple repeatInterval: ( grailsApplication.config.searchInterval ?: 1 ) * 60 * 1000,
            startDelay: ( grailsApplication.config.searchInterval ?: 1 ) * 60 * 1000
    }


    def execute() {
        if ( grailsApplication.config.searchInterval ) {
            GParsPool.withPool {
                grailsApplication.config.imagerySources.eachParallel {
                    if ( it.value.search ) {
                        searchService.automated( it.key )
                    }
                }
            }
        }
    }
}
