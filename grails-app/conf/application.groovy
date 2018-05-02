grails.converters.json.default.deep = true

grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.tdnsecuredrest.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.tdnsecuredrest.UserAuthority'
grails.plugin.springsecurity.authority.className = 'com.tdnsecuredrest.Authority'
grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
grails.plugin.springsecurity.interceptUrlMap = [
        [pattern: '/',               access: ['permitAll']],
        [pattern: '/error',          access: ['permitAll']],
        [pattern: '/index',          access: ['permitAll']],
        [pattern: '/index.gsp',      access: ['permitAll']],
        [pattern: '/shutdown',       access: ['permitAll']],
        [pattern: '/assets/**',      access: ['permitAll']],
        [pattern: '/**/js/**',       access: ['permitAll']],
        [pattern: '/**/css/**',      access: ['permitAll']],
        [pattern: '/**/images/**',   access: ['permitAll']],
        [pattern: '/**/favicon.ico', access: ['permitAll']],
        [pattern: '/api/login',      access: ['permitAll']],
        [pattern: '/api/register',   access: ['permitAll']],
        [pattern: '/api/apps/all',      access: ['ROLE_ADMIN']],
        [pattern: '/api/apps/**/approve',      access: ['ROLE_ADMIN']],
        [pattern: '/api/logout',     access: ['isFullyAuthenticated()']],
        [pattern: '/**',             access: ['permitAll']]
]
grails.plugin.springsecurity.filterChain.chainMap = [
        [pattern: '/api/register', filters: 'JOINED_FILTERS,-authenticationProcessingFilter,-exceptionTranslationFilter,-securityContextPersistenceFilter'],
        [pattern: '/api/**', filters:'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter'],
        [pattern: '/**', filters:'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter']
]

grails.plugin.springsecurity.rest.logout.endpointUrl = '/api/logout'
grails.plugin.springsecurity.rest.token.validation.useBearerToken = true
grails.plugin.springsecurity.rest.token.validation.headerName = 'Authorization'


grails.gorm.default.mapping = {
    autowire true
}

