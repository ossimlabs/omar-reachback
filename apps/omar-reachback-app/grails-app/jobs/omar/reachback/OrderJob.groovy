package omar.reachback

class OrderJob {

    def orderService


    static triggers = {
        simple repeatInterval: ( grailsApplication.config.orderInterval ?: 1 ) * 60 * 1000
    }


    def execute() {
        if ( grailsApplication.config.orderInterval ) {
            //orderService.automated()
        }
    }
}
