package tdn.api

import com.tdnsecuredrest.Follows
import com.tdnsecuredrest.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import org.grails.web.json.JSONElement
import org.springframework.security.access.prepost.PostAuthorize

import javax.annotation.security.RolesAllowed

class UserController {

    static responseFormats = ['json', 'xml']
    transient springSecurityService
    static transients = ['springSecurityService']

    JSONElement getUser(User u) {
        def json = JSON.parse((u as JSON).toString())
        json.put("isFollowing", Follows.countByFromAndTo(User.get(springSecurityService.principal.id), u) > 0)
        return(json)
    }

    def show() {
        respond User.get(springSecurityService.principal.id)
    }

    def user(Long id) {
        respond getUser(User.get(id))
    }

    def delete(Long id) {
        def user = User.get(id)
        user.enabled = false
        user.save(flush:true, failOnError: true)
        render status: 204
    }

    def update(User user) {
        if (user.id == springSecurityService.principal.id) {
            user.save(flush: true, failOnError: true)
            respond status: 204
        } else {
            respond status: 403
        }
    }
}
