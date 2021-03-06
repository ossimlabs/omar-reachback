package omar.reachback.app


import groovy.json.JsonOutput
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation


@Api(value = "/index",
	 description = "OMAR Reachback REST API"
)


class IndexController {

	def orderService
	def searchService


	def order() {
		def results = orderService.order( params )


		render results
	}


	@ApiOperation(
		httpMethod = "GET",
		produces = 'json',
		value = "Set search parameters with the URL."
	)
	@ApiImplicitParams([
		@ApiImplicitParam(
			dataType = 'string',
			defaultValue = '',
			name = 'bbox',
			paramType = 'query',
			value = 'Sets the boundes fpr the search in min_lon, min_lat, max_lon, max_lat format.'
		),
		@ApiImplicitParam(
			dataType = 'string',
			defaultValue = '',
			name = 'endDate',
			paramType = 'query',
			value = 'Sets the end date for the search.'
		),
		@ApiImplicitParam(
			dataType = 'integer',
			defaultValue = '',
			name = 'maxFeatures',
			paramType = 'query',
			value = 'Sets the maximum results returned.'
		),
		@ApiImplicitParam(
			dataType = 'integer',
			defaultValue = '',
			name = 'niirs',
			paramType = 'query',
			value = 'Sets the minimum NIIRS for the search.'
		),
		@ApiImplicitParam(
			dataType = 'string',
			defaultValue = '',
			name = 'sensors',
			paramType = 'query',
			value = 'Comma separated list of sensor IDs.'
		),
		@ApiImplicitParam(
			dataType = 'string',
			defaultValue = '',
			name = 'startDate',
			paramType = 'query',
			value = 'Sets the start date for the search.'
		)
	])
	def search() {
		def results = []
		grailsApplication.config.imagerySources.each {
			params.sourceName = it.key
			results += searchService.search( params )
		}

		try {
			render( contentType: 'application/json', text: JsonOutput.toJson( results ) )
		}
		catch( IllegalArgumentException event ) {
			render null
			log.error(event.toString())
		}
	}
}
