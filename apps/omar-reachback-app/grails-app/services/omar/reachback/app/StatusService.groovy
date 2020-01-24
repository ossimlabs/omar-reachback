package omar.reachback.app


import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper


@Transactional
class StatusService {

    def grailsApplication
    def httpService


    def checkForExistence() {
        def image = Image.findByStatusLike( "UNCHECKED" )
        if ( !image ) {
            return
        }

        image.status = "CHECKING"
        image.save()
        println "Checking if ${ image.imageId } alreadys exists..."

        def wfsParams = [
            URLEncoder.encode( "filter=image_id LIKE '${ image.imageId }'" ),
            'maxFeatures=1',
            'outputFormat=JSON',
            'request=getFeature',
            'service=WFS',
            'typeName=omar:raster_entry',
            'version=1.1.0'
        ]
        def url = "${ grailsApplication.config.o2WfsUrl }?${ wfsParams.join( '&' ) }"
        println URLDecoder.decode( url )

        def text = httpService.http( url )
        try {
            def json = new JsonSlurper().parseText( text )
            def numberOfFeatures = json.features.size()
            if ( numberOfFeatures > 0 ) {
                println "Found ${ image.imageId } !"
                image.status = "INGESTED"
            }
            else {
                println "Nope, ${ image.imageId } hasn't been ingested yet..."
                image.status = "PENDING"
            }
            image.save()
        }
        catch ( Exception event ) {
            println event


            return null
        }
    }
}
