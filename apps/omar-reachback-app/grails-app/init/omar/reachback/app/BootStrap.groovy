package omar.reachback.app


import groovyx.gpars.GParsPool


class BootStrap {

    def grailsApplication
    def searchService

    def init = { servletContext ->
        GParsPool.withPool {
            grailsApplication.config.imagerySources.eachParallel {
                if ( it.value.historicalSearch ) {
                    searchService.historical( it.key )
                }
            }
        }
    }

    def destroy = {
    }
}
