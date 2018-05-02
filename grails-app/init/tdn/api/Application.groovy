package tdn.api

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableAutoConfiguration
@ComponentScan
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {

        GrailsApp.run(Application, args)
    }
}