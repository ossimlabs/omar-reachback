package omar.reachback


class IndexController {

	def searchService


	def download() {
		render "N/A"
	}

	def search() {
		render searchService.search( params )
	}
}
