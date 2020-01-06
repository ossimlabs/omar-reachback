package omar.reachback

class HistoricalJob {

    def searchService


    static triggers = {
        simple repeatCount: 0
    }


    def execute() {
        if ( grailsApplication.config.historicalSearch ) {
            searchService.historical()
        }
    }
}
