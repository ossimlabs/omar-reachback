package omar.reachback

class IndexController {

	def searchService


	def download() {
		render "N/A"
	}

	def search() {
		def results = searchService.search( params )


		render results
	}
}
