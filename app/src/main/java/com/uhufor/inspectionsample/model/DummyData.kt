package com.uhufor.inspectionsample.model

object DummyData {
    fun getPersonList(): List<Person> {
        return listOf(
            Person("John Doe", "Android Developer", 28),
            Person("Jane Smith", "iOS Developer", 32),
            Person("Peter Jones", "Web Developer", 25),
            Person("Susan Williams", "Project Manager", 41),
            Person("David Brown", "UI/UX Designer", 29),
            Person("Karen Miller", "QA Engineer", 35),
            Person("Michael Wilson", "Backend Developer", 31),
            Person("Linda Taylor", "DevOps Engineer", 38),
            Person("Robert Anderson", "Data Scientist", 33),
            Person("Patricia Thomas", "Product Owner", 45)
        )
    }
}
