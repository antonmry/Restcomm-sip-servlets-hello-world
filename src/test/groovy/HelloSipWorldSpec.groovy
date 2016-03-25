import com.galiglobal.hellosipworld.HelloSipWorld

class HelloSipWorldSpec extends spock.lang.Specification {
    def "hellosipworld"() {
        given: "a new HelloSipWorld class is created"
        def helloSipWorld = new HelloSipWorld();

        expect: "The new instance is not null"
        helloSipWorld != null;
    }
}
