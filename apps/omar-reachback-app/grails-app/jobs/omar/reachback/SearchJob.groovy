package omar.reachback

class SearchJob {

    def searchService


    static triggers = {
        simple repeatInterval: ( grailsApplication.config.searchInterval ?: 1 ) * 60 * 1000,
            startDelay: ( grailsApplication.config.searchInterval ?: 1 ) * 60 * 1000
    }


    def execute() {
        if ( grailsApplication.config.searchInterval ) {
            searchService.automated()
        }
    }
}
