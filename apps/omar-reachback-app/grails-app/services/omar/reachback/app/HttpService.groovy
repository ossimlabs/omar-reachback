package omar.reachback.app


import grails.gorm.transactions.Transactional


@Transactional
class HttpService {

    def grailsApplication


    def http( url ) {
        def curlCommand = "curl -b cookie -c cookie -L"

        def certFile = grailsApplication.config.certs.certFile
        def keyFile = grailsApplication.config.certs.keyFile
        if ( certFile && keyFile ) {
            curlCommand += " -E ${ certFile } -k --key ${ keyFile }"
        }

        curlCommand += " '${ url }'"

        def curlFile = File.createTempFile( "curl", ".sh" )
        curlFile.write( curlCommand )

        def command = [ "sh", curlFile.absolutePath ]
        def process = command.execute()

        def standardOut = new StringBuffer()
        def standardError = new StringBuffer()
        process.waitForProcessOutput( standardOut, standardError )

        def status = process.waitFor()
        if ( status == 0 ) { return standardOut.toString() }
        else { return standardError }
    }
}
