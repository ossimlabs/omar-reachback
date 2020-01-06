package omar.reachback


class Image {

    Date date = new Date()
    String imageId
    String status = 'UNCHECKED'
    String uid


    static constraints = {
        imageId nullable: true
    }

    static mapping = {
        version false
    }
}
