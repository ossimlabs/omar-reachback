package omar.reachback.app

class StatusJob {

    def statusService


    static triggers = {
        simple repeatInterval: 10 * 1000
    }


    def execute() {
        statusService.checkForExistence()
    }
}
