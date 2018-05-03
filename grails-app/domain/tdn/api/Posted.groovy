package tdn.api

import com.tdnsecuredrest.User
import grails.neo4j.Relationship
import grails.persistence.Entity

@Entity
class Posted implements Relationship<User, Post> {

    static constraints = {
    }

    static mapWith = "neo4j"
}
