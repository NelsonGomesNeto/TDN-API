package com.tdnsecuredrest

import grails.neo4j.Relationship
import grails.gorm.annotation.Entity

@Entity
class Follows implements Relationship<User, User> {

    static constraints = {
    }

    static mapWith = "neo4j"
}
