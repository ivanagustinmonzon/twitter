# twitter-kata


# Description #
The goal of this kata is to practice Clean Architecture driven by Outside-in TDD.

You will build a basic Twitter application following a set of restrictions on each iteration. Try to apply DRY, KISS, and SOLID principles during the process.

**Recommendation:** create a Maven project using IntelliJ IDEA.

## Restrictions 
- Write the best code you can, while keeping it simple.
- Don't do more than the iteration asks.
- Don't add infrastructure if the functionality doesn't explicitly ask for it.
- Don't rely on libraries if the functionality doesn't explicitly ask for it. 

### Iteration 1

* A user can register with his real name and a nickname. Eg: Jack Bauer, `@jack`.

* If another person has been already registered using the same nickname an error is expected.

### Iteration 2

* The user can update his real name.

### Iteration 3

* A user can follow other users. The nickname of the other user is all it needs to follow it. 

* Anyone can ask who is following who, just knowing the nickname

:warning: From this point on, you should not modify the code written in previous iterations :warning:
---

### Iteration 4

* The records of the users and the followed users must be stored in a durable form. (Discuss with your onboarding buddy about the current technologies in use)

### Iteration 5

* Create an HTTP delivery mechanism that allows accessing all the functionalities developed so far. (Discuss with your onboarding buddy about the current technologies in use)

### Iteration 6

* A user can tweet. Other users can read all tweets of a user knowing his username.


#### Assumptions
 - A user can register any nickname (not just '@' starting)
 - A user cannot change its nickname
 - Persistence shared across multiple fibers (concurrent)
 - Cannot store user list of followers/following in a single item (scalability). 
    i.e following and followed are separated entities in persistence

# Persistence model 
    
### Entities and relationships

    User{
        nickname
        realname
    }

    Follow{
        follower_id
        followee_id
    }

    users - 0..n -> followees
    users - 0..n -> followers

### access patterns

    User:
        - register user                     = PK=USER#johnbauer1 , SK=USER#johnbauer1
        - update user                       = PK=USER#johnbauer1 , SK=USER#johnbauer1
    Follow:
        - follow a user                     = PK=USER#janedoe    , SK=FOLLOWED#johnbauer
        - find all followees from a user    = PK=USER#johnbauer1 , SK=FOLLOWED#
    Tweet*:
        - A user can tweet                  = PK=USER#<nickname> , SK=TWEET#<tweet-id>
        - Read tweets from user             = PK=USER#<nickname> , SK=TWEET#

### data modeling

| Entity   | Paritition key (PK) |        Sort key (SK) |                              Attributes |
|----------|:-------------------:|---------------------:|----------------------------------------:|
| User     |  USER#\<nickname>   |     USER#\<nickname> |                        realname: String |
| Followed |  USER#\<nickname>   | FOLLOWED#\<nickname> |                       timestamp: String | max= 400kb
| Tweet*   |  USER#\<nickname>   |    TWEET#\<tweet-id> | message: String,<br/> timestamp: String |



| Paritition key (PK) |      Sort key (SK) |                   Attributes |
|:-------------------:|-------------------:|-----------------------------:|
|   USER#johnbauer1   |    USER#johnbauer1 |                   John Bauer |
|  USER#juliarobers2  |  USER#juliarobers2 |                 Julia Robers |
|  USER#juliarobers2  | FOLLOWED#johnbauer |                 202204071222 |
|  USER#johnbauer1*   |       TWEET#123abc | Hello twitter!, 202204071322 |


## Presentation model 

Register user: 
```
POST /users/
  {
    nickname: string,
    realname: string
  }
  
  returns:
    201: created
    409: user already registered
    500: service error
```
Update user: 
```
PUT /users/<nickname>
  {
    realname: string
  }
  
  returns:
    204: updated
    404: user not found 
    500: service error
```

Follow user: 
```
POST /users/<nickname>/follows
  {
    followeeId: string
  }
  
  returns:
    204: ok
    404: user not found 
    500: service error
```

Who is following who: 
```
GET /users/<nickname>/follows
  {}
  
  returns:
    200: [ {
      - nickname: string,
      - realname: string
    } ]
    404: user not found
    500: service error
```



To-Do:
- [DONE] Terminar la relacion el modelado de base
- [DONE] Levantar docker
- [DONE] Conectar una cli para tirar comandos (o UI)
- [DONE] Crear tablas en Dynamolocal mediante los tests
- [DONE] Conectar las queries de usuarios y follows con DynamoDB
- [DONE] Test integracion
- [DONE] Implementar capa de presentacion http con http4s
- [INPR] Implementar Tweet feature
- CI : Gitlab
- Dockerizar
- [CANCEL] Implementar vista (React?)
- Definir configuracion (ciris?) PURE CONFIG 

NTH:
- Unificar Category index para tener las distintas extensiones
- Definir una Query.empty[FooIndex] que solo empiece con FOO# (reemplazar la actual)
- Correr it tests en paralelo

Elegi agregar los codecs en el dominio vs crear vistas del dominio en la capa de presentacion
