package omar.reachback.app


class Image {

    Date date = new Date()
    String imageId
    String source
    String status = 'UNCHECKED'
    String uid


    static constraints = {
        imageId nullable: true
    }

    static mapping = {
        version false
    }
}
