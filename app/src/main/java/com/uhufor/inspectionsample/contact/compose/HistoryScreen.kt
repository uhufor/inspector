package com.uhufor.inspectionsample.contact.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Experience(val id: Int, val title: String, val dates: String, val description: String)

val sampleExperiences = listOf(
    Experience(
        1,
        "Senior Software Engineer - Tech Solutions Inc.",
        "March 2020 - Present",
        "- Lead development of Android applications.\n- Design and implement new features."
    ),
    Experience(
        2,
        "Software Engineer - Innovatech Ltd.",
        "July 2018 - February 2020",
        "- Developed and maintained mobile applications.\n- Collaborated with cross-functional teams."
    ),
    Experience(
        3,
        "Junior Developer - Startup Coders LLC",
        "January 2017 - June 2018",
        "- Contributed to early-stage mobile service development.\n- Implemented features based on user feedback."
    ),
    Experience(
        4,
        "Software Development Intern - Future Systems Co.",
        "June 2016 - December 2016",
        "- Assisted in software testing and QA processes.\n- Supported development documentation."
    ),
    Experience(
        5,
        "Android Developer - MobileFirst Corp.",
        "May 2015 - May 2016",
        "- Focused on native Android app development.\n- Participated in code reviews and agile sprints."
    ),
    Experience(
        6,
        "QA Engineer - TestPerfect Inc.",
        "August 2014 - April 2015",
        "- Executed test cases and reported bugs.\n- Developed automated test scripts."
    ),
    Experience(
        7,
        "Technical Support Specialist - HelpDesk Pro",
        "January 2014 - July 2014",
        "- Provided technical assistance to customers.\n- Troubleshot software and hardware issues."
    ),
    Experience(
        8,
        "Web Developer (Part-Time) - Creative Web Designs",
        "September 2013 - December 2013",
        "- Developed and maintained client websites.\n- Worked with HTML, CSS, and JavaScript."
    ),
    Experience(
        9,
        "IT Intern - Global Tech Innovations",
        "June 2013 - August 2013",
        "- Supported IT infrastructure and network maintenance.\n- Assisted with hardware and software upgrades."
    ),
    Experience(
        10,
        "Freelance Mobile Developer - Self-Employed",
        "January 2012 - May 2013",
        "- Developed small mobile apps for local businesses.\n- Managed all project phases from concept to deployment."
    )
)

@Composable
fun HistoryScreen(experiences: List<Experience>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                text = "Work Experience",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        items(experiences) { experience ->
            ExperienceItem(experience)
            if (experiences.last() != experience) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }
        }
    }
}

@Composable
fun ExperienceItem(experience: Experience) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = experience.title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = experience.dates,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
        )
        Text(
            text = experience.description,
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultHistoryPreview() {
    HistoryScreen(experiences = sampleExperiences)
}
