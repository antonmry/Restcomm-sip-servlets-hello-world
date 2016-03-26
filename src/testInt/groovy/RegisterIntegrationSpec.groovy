import com.galiglobal.siptester.SipClient
import spock.lang.*

class RegisterIntegrationSpec extends spock.lang.Specification {

    //FIXME: this shouldn't be environment dependent
    @Shared sipClient = new SipClient('172.18.0.2', 5080, '172.18.0.1', 5089)

    def "SIP Registration"() {
        setup:

        expect:
        assert sipClient.register(mySipURI, contact, expiration)

        cleanup:
        sipClient.register(mySipURI, contact, 0)

        where:
        mySipURI | contact | expiration
        "sip:test_client@example.com" | "sip:test_client@example.com" | 3600
    }

}
