package com.example.myapplicationcityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplicationcityapp.ui.theme.MyApplicationcityappTheme
import androidx.compose.foundation.Image
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import java.util.logging.Logger.global

data class PlaceDetails(
    val name: String,
    val description: String,
    val imageRes: Int,
    val longDescription: String,
    val address: String,
    val rating: Float
)


val allPlaceDetails = mapOf(
    "Coffee Shops" to mapOf(
        "XPRESSO" to PlaceDetails(
            name = "XPRESSO",
            description = "Popular global coffee chain",
            imageRes = R.drawable.xpressco,
            longDescription = "XPRESSO is a popular coffee shop known for its wide variety of coffee drinks and relaxed atmosphere. It offers a perfect blend of traditional and modern coffee experiences.",
            address = "123 Makati Ave, Makati City",
            rating = 4.5f
        ),
        "DEUCES" to PlaceDetails(
            name = "DEUCES",
            description = "Local Philippine coffee shop",
            imageRes = R.drawable.deuces,
            longDescription = "DEUCES is a charming local coffee shop that prides itself on serving high-quality, locally sourced coffee. It offers a cozy environment perfect for work or casual meetups.",
            address = "456 Ayala Ave, Makati City",
            rating = 4.7f
        ),
        "Three Squares cafe+bar" to PlaceDetails(
            name = "Three Squares cafe+bar",
            description = "Hybrid cafe and bar",
            imageRes = R.drawable.threesquares,
            longDescription = "Three Squares cafe+bar transitions seamlessly from a cozy daytime cafe to a vibrant bar in the evening. They are known for their delightful cold brews and comfort food options.",
            address = "789 Salcedo St, Makati City",
            rating = 4.6f
        ),
        "NATSU" to PlaceDetails(
            name = "NATSU",
            description = "Japanese-inspired coffee shop",
            imageRes = R.drawable.natsu,
            longDescription = "NATSU combines minimalist Japanese design with exceptional coffee craftsmanship. They specialize in matcha lattes and seasonal coffee offerings.",
            address = "101 Paseo de Roxas, Makati City",
            rating = 4.8f
        ),
        "Ani Cafe" to PlaceDetails(
            name = "Ani Cafe",
            description = "Eco-friendly cafe",
            imageRes = R.drawable.anicafe,
            longDescription = "Ani Cafe focuses on sustainability with a plant-based menu and ethically sourced coffee. A perfect destination for eco-conscious patrons.",
            address = "202 Legazpi St, Makati City",
            rating = 4.6f
        )
    ),

    "Restaurants" to mapOf(
        "La latina" to PlaceDetails(
            name = "La latina",
            description = "Authentic Latin American cuisine",
            imageRes = R.drawable.lalatina,
            longDescription = "known for their tacos, areas, and refreshing margaritas, this restaurant is perfect for an adventurous palate.",
            address = "789 Latin St, Makati City",
            rating = 4.7f
        ),
        "Filling Station" to PlaceDetails(
            name = "Filling Station",
            description = "Retro-themed diner",
            imageRes = R.drawable.fillingstation,
            longDescription = "Step back into the 1950s at the Filling Station, a nostalgic diner serving classic American comfort food. Known for its milkshakes, burgers, and quirky decor.",
            address = "123 Nostalgia Ave, Makati City",
            rating = 4.6f
        ),
        "Almacen Cantina" to PlaceDetails(
            name = "Almacen Cantina",
            description = "Modern Mexican cantina",
            imageRes = R.drawable.almacen,
            longDescription = "Almacen Cantina serves contemporary takes on Mexican classics. Try their creative tacos and handcrafted cocktails in a lively atmosphere.",
            address = "456 Fiesta Rd, Makati City",
            rating = 4.8f
        ),
        "Tetsuo" to PlaceDetails(
            name = "Tetsuo",
            description = "Japanese fusion cuisine",
            imageRes = R.drawable.tetsuo,
            longDescription = "Tetsuo combines Japanese flavors with modern flair. Highlights include karaage rice bowls and unique cocktails in a sleek, industrial setting.",
            address = "789 Urban Way, Makati City",
            rating = 4.6f
        ),
        "Ha Noi Pho" to PlaceDetails(
            name = "Ha Noi Pho",
            description = "Traditional Vietnamese restaurant",
            imageRes = R.drawable.hanoipho,
            longDescription = "Ha Noi Pho specializes in authentic Vietnamese dishes. Their flavorful pho and fresh spring rolls bring the vibrant tastes of Vietnam to life.",
            address = "101 Asia Lane, Makati City",
            rating = 4.9f
        ),
    ),

    "Kid-Friendly Places" to mapOf(
        "Dreamlab Business Simulation" to PlaceDetails(
            name = "Dreamlab Business Simulation",
            description = "Interactive learning space for kids",
            imageRes = R.drawable.dreamlab,
            longDescription = "Dreamlab Business Simulation offers an engaging environment where kids can role-play various professions and learn essential skills in a fun, interactive way. It's a unique blend of education and entertainment, perfect for curious young minds.",
            address = "Level 3, EduPlay Center, Ayala Mall, Makati City",
            rating = 4.5f
        ),
        "Salcedo Village" to PlaceDetails(
            name = "Salcedo Village",
            description = "Community park with kid-friendly attractions",
            imageRes = R.drawable.salcedo,
            longDescription = "Salcedo Village features a lively weekend market, open spaces for play, and shaded areas for picnics. It's a family-friendly destination ideal for outdoor activities and exploring local crafts and food.",
            address = "Tordesillas St, Salcedo Village, Makati City",
            rating = 4.7f
        ),
        "Fantasy World-One Ayala Makati" to PlaceDetails(
            name = "Fantasy World-One Ayala Makati",
            description = "Whimsical indoor play area",
            imageRes = R.drawable.fantasy,
            longDescription = "Fantasy World at One Ayala Makati is an imaginative indoor playground filled with themed zones, fun activities, and vibrant decor. It's designed to captivate children's imaginations and provide hours of entertainment.",
            address = "One Ayala Mall, Makati City",
            rating = 4.6f
        ),
        "Kinder City-Makati" to PlaceDetails(
            name = "Kinder City-Makati",
            description = "Interactive and safe play area for kids",
            imageRes = R.drawable.kindercity,
            longDescription = "Kinder City offers a secure and interactive environment for kids to engage in pretend play, physical activities, and creative learning. With attentive staff and exciting facilities, it's a favorite among parents and children alike.",
            address = "Level 2, Makati Center, Makati City",
            rating = 4.8f
        ),
        "Timezone Play N Learn" to PlaceDetails(
            name = "Timezone Play N Learn",
            description = "Family-friendly arcade and play zone",
            imageRes = R.drawable.timezone,
            longDescription = "Timezone Play N Learn combines the excitement of arcade games with educational and physical play zones. It’s a fun-filled destination for families, offering activities for kids and adults to enjoy together.",
            address = "Glorietta 4, Makati City",
            rating = 4.6f
        ),
    ),

    "Parks" to mapOf(
        "Dreamlab Business Simulation" to PlaceDetails(
            name = " ",
            description = " ",
            imageRes = R.drawable.dreamlab,
            longDescription = " ",
            address = " ",
            rating = 4.5f
        ),
        "Salcedo Village" to PlaceDetails(
            name = "Salcedo Village",
            description = " ",
            imageRes = R.drawable.salcedo,
            longDescription = " ",
            address = "",
            rating = 4.7f
        ),
        "Fantasy World-One Ayala Makati" to PlaceDetails(
            name = "Fantasy World-One Ayala Makati",
            description = "",
            imageRes = R.drawable.fantasy,
            longDescription = "",
            address = "",
            rating = 4.6f
        ),
        "Kinder City-Makati" to PlaceDetails(
            name = "Kinder City-Makati",
            description = "",
            imageRes = R.drawable.kindercity,
            longDescription = "",
            address = "",
            rating = 4.8f
        ),
        "Timezone Play N Learn" to PlaceDetails(
            name = "Timezone Play N Learn",
            description = "",
            imageRes = R.drawable.timezone,
            longDescription = " ",
            address = " ",
            rating = 4.6f
        ),
    ),

    // Add other category maps...
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationcityappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MakatiCityApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakatiCityApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController)
        }
        composable("recommendations/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            RecommendationsScreen(category ?: "", navController)
        }
        composable("detail/{category}/{name}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            val name = backStackEntry.arguments?.getString("name")
            DetailScreen(category ?: "", name ?: "", navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Makati City",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        CategoryList(modifier = Modifier.padding(innerPadding), navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryList(modifier: Modifier = Modifier, navController: NavHostController) {
    val categories = listOf(
        Pair("Coffee Shops", R.drawable.coffeemain),
        Pair("Restaurants", R.drawable.restuarantmain),
        Pair("Kid-Friendly Places", R.drawable.kidfriendlymain),
        Pair("Parks", R.drawable.parkmain),
        Pair("Shopping Centers", R.drawable.shoppingmain)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(categories) { (category, imageRes) ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                onClick = { navController.navigate("recommendations/$category") }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = category,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.surface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(category: String, navController: NavHostController) {
    val recommendations = when (category) {
        "Coffee Shops" -> listOf(
            Triple("XPRESSO", "Popular global coffee chain", R.drawable.xpressco),
            Triple("DEUCES", "Local Philippine coffee shop", R.drawable.deuces),
            Triple("Three Squares cafe+bar", "The Coffee Bean & Tea Leaf", R.drawable.threesquares),
            Triple("NATSU", "Filipino coffee shop chain", R.drawable.natsu),
            Triple("Ani Cafe", "Australian specialty coffee roaster", R.drawable.anicafe)
        )

        "Restaurants" -> listOf(
            Triple("La latina", "A vibrant eatery serving authentic Latin American cuisine.", R.drawable.lalatina),
            Triple("Filling Station", "A retro-themed diner with an extensive menu of American classics. Known for its nostalgic 1950s decor, Filling Station is famous for its milkshakes and hearty burgers", R.drawable.fillingstation),
            Triple("Almacen Cantina", "A contemporary cantina offering a modern twist on Mexican dishes. Their creative tacos and handcrafted cocktails make Almacen a standout spot for foodies. With a lively atmosphere, it’s perfect for group outings", R.drawable.almacen),
            Triple("Tetsuo", "A Japanese fusion restaurant with a sleek, industrial vibe. Tetsuo’s menu highlights include their karaage rice bowls and unique cocktails infused with Japanese flavors. Its urban aesthetic makes it a great place to unwind", R.drawable.tetsuo),
            Triple("Ha Noi Pho", "A Vietnamese restaurant specializing in pho and other traditional dishes. Ha Noi Pho’s fresh ingredients and authentic recipes bring the taste of Vietnam to your plate. Their spring rolls and banh mi are also must-tries", R.drawable.hanoipho)
        )

        "Kid-Friendly Places" -> listOf(
            Triple("Dreamlab Business Simulation", "Interactive learning space for kids", R.drawable.dreamlab),
            Triple("Salcedo Village", "Community park with kid-friendly attractions", R.drawable.salcedo),
            Triple("Fantasy World-One Ayala Makati", "Whimsical indoor play area", R.drawable.fantasy),
            Triple("Kinder City-Makati", "Interactive and safe play area for kids", R.drawable.kindercity),
            Triple("Timezone Play N Learn", "Family-friendly arcade and play zone", R.drawable.timezone)
        )

        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        category,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(recommendations) { (name, description, imageRes) ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    onClick = {
                        navController.navigate("detail/$category/$name")
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = name,
                            modifier = Modifier
                                .size(120.dp)
                                .padding(8.dp),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(category: String, name: String, navController: NavHostController) {
    val place = allPlaceDetails[category]?.get(name) ?: return
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        place.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = place.imageRes),
                contentDescription = place.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = place.longDescription,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Address: ${place.address}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Rating: ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = place.rating.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Rating",
                    tint = Color.Yellow
                )
            }
        }
    }

}


@Preview(showBackground = true)
@Composable
fun MakatiCityAppPreview() {
    MyApplicationcityappTheme {
        MakatiCityApp()
    }
}