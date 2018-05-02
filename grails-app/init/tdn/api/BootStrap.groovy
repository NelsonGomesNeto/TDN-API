package tdn.api

import com.tdnsecuredrest.Authority
import com.tdnsecuredrest.User
import com.tdnsecuredrest.UserAuthority
import grails.converters.JSON
import neo4j.movies.Person

class BootStrap {

    def init = { servletContext ->

        TimeZone.setDefault(TimeZone.getTimeZone('UTC'))
        if (Authority.findByAuthority('ROLE_USER') == null) {
            new Authority(authority: 'ROLE_USER').save flush: true
        }
        if (Authority.findByAuthority('ROLE_ADMIN') == null) {
            new Authority(authority: 'ROLE_ADMIN').save flush: true
        }
        if (User.findByUsername("senpai") == null) {
            def user1 = new User(name: "Senpai", username:"senpai", password:"nil", bornDate: new Date(), email: "senpai@hentai.net").save(flush: true, failOnError: true)
            UserAuthority.create(user1,Authority.findByAuthority("ROLE_ADMIN"))
            UserAuthority.create(user1, Authority.findByAuthority('ROLE_USER'))
        }

        JSON.registerObjectMarshaller(User) {
            def output = [:]
            output['id'] = it.id
            output['username'] = it.username
            output['name'] = it.name
            output['avatar'] = it.avatar
            output['background'] = it.background
            output['bornDate'] = it.bornDate
            output['authority'] = it.authorities
            return output
        }
    }
    def destroy = {

    }

}
