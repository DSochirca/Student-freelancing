# Welcome to the **Student Freelancing** project of group 3B!

Our system, **Student Freelancing**, was designed to allow TU Delft students to provide services for companies in the Netherlands with ease. <br><br>_Students_ can post the services they want to provide, and _companies_ can search for them by selecting an expertise they are interested in or by specifying certain keywords. If a _company_ is interested in a student then it can submit a service request to them and wait for them to accept or deny it. Also, _companies_ can post a request for services without targeting specific students, and in this case students will be the ones signing up to them. If 2 parties agree on an offer, then the system will generate a _contract_. This contract can be extended, modified and terminated. At the end of the service period, both companies and students can provide _feedback_ about their working experience, which will be visible to all users.

## Description of project

**Student Freelancing** is developed by students from **TU Delft** to make freelancing easier and more accesible. Our system uses a powerful and comprehensive **REST API built with Spring Boot**, and **Eureka + Zuul** for _service discovery_ and _intelligent routing_. It was built **modular** such that it can be extended with extra functionalities later, and as an API it allows for **easy integration with other systems**.

Our project may have reached a working version with all required features implemented, but we're not stopping there! We have a ton of other innovative ideas to add to our system, such as having recommendations, better security (HTTPS, TU Delft SSO, 2FA), and controlling spam. So take it out for a spin and let us know how we can make it your favorite app!

## Group members

| 📸                                                                                        | Name | Email |
|-------------------------------------------------------------------------------------------|---|---|
| ![](https://gitlab.ewi.tudelft.nl/uploads/-/system/user/avatar/3816/avatar.png?width=400) | Dan Sochirca | D.Sochirca@student.tudelft.nl |
| ![](https://gitlab.ewi.tudelft.nl/uploads/-/system/user/avatar/2388/avatar.png?width=400) | Antonios Barotsis | a.barotsis@student.tudelft.nl |
| ![](https://gitlab.ewi.tudelft.nl/uploads/-/system/user/avatar/3105/avatar.png?width=400) | Nathan Klumpenaar | M.J.N.Klumpenaar@student.tudelft.nl |
| ![](https://gitlab.ewi.tudelft.nl/uploads/-/system/user/avatar/3550/avatar.png?width=400) | Rado Todorov | R.A.Todorov@student.tudelft.nl |
| ![](https://gitlab.ewi.tudelft.nl/uploads/-/system/user/avatar/3681/avatar.png?width=400) | Miloš Ristić | m.ristic-1@student.tudelft.nl |
## How to run it

Firstly, **clone** the repository or **download** the source code. Then open the repository in you preferred **IDE** and **build the project**. Once the project's build is done, you should first run the eureka server, then all the microservices: usersService, offersRequestsService, contractService, feedbackService. We also have a detailed description of all endpoints in our [wiki](https://gitlab.ewi.tudelft.nl/cse2115/2021-2022/sem-group-03b/sem-repo-03b/-/wikis/Endpoint%20wiki).

In order to safely run our product on a production environment, you will need to create 2 `.env` files:
 - `userService/src/resources/.env` containing:
   - `JWT_SECRET`: the secret key used to sign the JWT token. (defaults to `secret`)
   - `ADMIN_PASSWORD`: the password of the admin that is created on first start. (defaults to `admin`)
   - `JWT_LIFETIME`: Optional. The lifetime of the JWT token in seconds. (defaults to `3600`)
 - `eurekaServer/src/resources/.env` containing:
   - `JWT_SECRET`: the secret key used to validate the the JWT token signed by the userService. (defaults to `secret`)
   - `JWT_LIFETIME`: Optional. The lifetime of the JWT token in seconds. (defaults to `3600`)
   
[Read more about our .env files](https://gitlab.ewi.tudelft.nl/cse2115/2021-2022/sem-group-03b/sem-repo-03b/-/wikis/Envloader).

[Read more about how we handle authentication](https://gitlab.ewi.tudelft.nl/cse2115/2021-2022/sem-group-03b/sem-repo-03b/-/wikis/Authentication).

## How to contribute to it
The main purpose of this repository is to continue improving it. We want to make contributing to this project as easy and transparent as possible, and we are grateful to the community for contributing bug fixes and improvements. Read below to learn how you can take part in improving Student Freelancing.

### [Code of Conduct](CODE_OF_CONDUCT.md)
We have adopted a Code of Conduct that we expect project participants to adhere to. Please read the [full text](CODE_OF_CONDUCT.md) so that you can understand what actions will and will not be tolerated.

There are many ways in which you can participate in the project, for example:
- Submit bugs and issues, and help us verify as they are checked in
- Review source code changes
- Review the documentation and make merge requests for anything from typos to new content

If you are interested in fixing issues and contributing directly to the code base, please read the **Contribution Guidelines** down below:

### Contribution Guidelines

Please ensure your merge request adheres to the following guidelines:

- Alphabetize your entry.
- Search previous merge requests before adding a new one, as yours may be a duplicate.
- Suggested READMEs should be beautiful or stand out in some way.
- Make an individual merge request for each suggestion.
- New categories, or improvements to the existing categorization are welcome.
- Keep descriptions short and simple, but descriptive.
- Start the description with a capital and end with a full stop/period.
- Check your spelling and grammar.
- Make sure your text editor is set to remove trailing whitespace.
- Test your code! Tests help us prevent regressions from being introduced to the codebase.

Thank you for your suggestions!
