package omar.reachback.app

class SearchJob {

    def searchService


    static triggers = {
        simple repeatInterval: ( grailsApplication.config.searchInterval ?: 1 ) * 60 * 1000,
            startDelay: ( grailsApplication.config.searchInterval ?: 1 ) * 60 * 1000
    }


    def execute() {
        if ( grailsApplication.config.searchInterval ) {
            grailsApplication.config.imagerySources.each {
                if ( it.value.search ) {
                    searchService.automated( it.key )
                }
            }
        }
    }
}
