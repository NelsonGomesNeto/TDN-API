package tdn.api

import com.tdnsecuredrest.Authority
import com.tdnsecuredrest.User
import com.tdnsecuredrest.UserAuthority
import grails.converters.JSON

class RegisterController {

    static allowedMethods = ["POST"]

    static responseFormats = ['json', 'xml']

    def index() { }


    def save(User user) {
        user.save(flush:true, failOnError: true)
        UserAuthority.create(user,  Authority.findByAuthority("ROLE_USER"))
        render(status: 201, user as JSON)
    }
}
