package models

enum class LoginStatus(val description: String?) {
    Idle(null),
    Loading("Loading..."),
    Failed("Login or password are incorrect or user does not exists"),
    Success(null)
}