package omar.reachback.app

class HistoricalJob {

    def searchService


    static triggers = {
        simple repeatCount: 0
    }


    def execute() {
        grailsApplication.config.imagerySources.each {
            if ( it.value.historicalSearch ) {
                searchService.historical( it.key )
            }
        }
    }
}
