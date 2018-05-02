package tdn.api

import com.tdnsecuredrest.Authority
import com.tdnsecuredrest.User
import com.tdnsecuredrest.UserAuthority
import grails.converters.JSON
import neo4j.movies.Person

import javax.xml.bind.ValidationException

class RegisterController {

    static allowedMethods = ["POST"]

    static responseFormats = ['json', 'xml']

    def index() { }

    def save(User user) {
        if (user.hasErrors()) {
            render(status: 409, ["This user already exists"] as JSON)
        } else {
            user.withTransaction {user.save(flush: true, failOnError: true)}
            UserAuthority.create(user,  Authority.findByAuthority("ROLE_USER"))
            render(status: 201, user as JSON)
        }
    }
}
