package tdn.api

import com.tdnsecuredrest.User
import grails.gorm.annotation.Entity
import grails.neo4j.Relationship

@Entity
class Likes implements Relationship<User, Post> {

    static constraints = {
    }

    static mapWith = "neo4j"
}
