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


