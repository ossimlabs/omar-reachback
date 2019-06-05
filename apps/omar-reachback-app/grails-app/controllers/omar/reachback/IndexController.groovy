package omar.reachback


import groovy.json.JsonOutput


class IndexController {

	def searchService


	def download() {
		render "N/A"
	}

	def search() {
		def results = searchService.search( params )


		render JsonOutput.toJson( results )
	}
}
