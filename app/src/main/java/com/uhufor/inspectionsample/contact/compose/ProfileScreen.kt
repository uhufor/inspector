package com.uhufor.inspectionsample.contact.compose

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.uhufor.inspectionsample.R
import com.uhufor.inspectionsample.ui.theme.InspectionSampleTheme

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val (profileImage, heartIcon, nameText, ageText, addressText, emailText) = createRefs()

        Image(
            painter = painterResource(id = R.drawable.ic_profile_placeholder),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(100.dp)
                .constrainAs(profileImage) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                },
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.ic_heart),
            contentDescription = "Heart Icon",
            modifier = Modifier
                .size(30.dp)
                .constrainAs(heartIcon) {
                    bottom.linkTo(profileImage.bottom)
                    end.linkTo(profileImage.end)
                }
                .clickable {
                    Toast.makeText(context, "Heart Clicked (Compose)", Toast.LENGTH_SHORT).show()
                }
        )

        Text(
            text = "John Doe",
            fontSize = 20.sp,
            color = Color(0xFFFC86BB),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.constrainAs(nameText) {
                top.linkTo(profileImage.top)
                start.linkTo(profileImage.end, margin = 16.dp)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = "Age: 35",
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.constrainAs(ageText) {
                top.linkTo(nameText.bottom, margin = 8.dp)
                start.linkTo(nameText.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = "Address: 123 Main St, Anytown USA",
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.constrainAs(addressText) {
                top.linkTo(ageText.bottom, margin = 8.dp)
                start.linkTo(nameText.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = "Email: john.doe@example.com",
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.constrainAs(emailText) {
                top.linkTo(addressText.bottom, margin = 8.dp)
                start.linkTo(nameText.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultProfilePreview() {
    InspectionSampleTheme {
        ProfileScreen()
    }
}
