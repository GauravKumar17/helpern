package com.example.helpern2.model

data class Service(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val rating: Double = 0.0,
    val price: Double = 0.0,
    val imageUrl: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

val sampleServices = listOf(
    Service("1", "Vivek", "Electrician", 4.5, 499.0, "https://media.gettyimages.com/id/117144768/photo/diy-disaster.jpg?s=612x612&w=gi&k=20&c=uT4IQbFpK8M4JaY2UPR9tjh61wQYtIYB6kuVWKstWCk=", "Expert in home wiring and repairs."),
    Service("2", "Raju", "Plumber", 4.8, 399.0, "https://imgs.search.brave.com/9wFmvHP_gM0gwYcYp5iGpo5Lxap7A0XBD15_bQ-AcQo/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly90aHVt/YnMuZHJlYW1zdGlt/ZS5jb20vYi9mdW5u/eS1wbHVtYmVyLTM5/Mjc4OTAuanBn", "Specialize in leak detection and pipe fixing."),
    Service("3", "Majnu", "Painter", 4.9, 699.0, "https://i.pinimg.com/736x/47/b0/d3/47b0d396c0d7979f5db89bac9caae312.jpg", "Eco-friendly cleaning services."),
    Service("4", "Amesh", "Barber", 4.2, 199.0, "https://www.shutterstock.com/image-photo/long-hair-freak-crazy-man-260nw-1725096565.jpg", "Men's haircut and grooming services."),
    Service("5", "Akshay", "Cleaner", 4.7, 599.0, "https://img.freepik.com/free-photo/smiling-holding-points-bucket-cleaning-tools-young-africanamerican-cleaner-male-uniform-with-gloves-isolated-green-background_141793-135154.jpg?semt=ais_hybrid&w=740&q=80", "Deep cleaning for homes and offices."),
    Service("6", "Sumit", "Electrician", 4.3, 449.0, "https://media.istockphoto.com/id/161654793/photo/electric-shock.jpg?s=612x612&w=0&k=20&c=YhmFS7UF2jNLsb32ZADqXXeSOTCZR-F0aN05Kt8MElk=", "Electrical appliance repair specialist."),
    Service("7", "Arun", "Cleaner", 4.9, 699.0, "https://thumbs.dreamstime.com/z/cleaning-services-black-cleaner-man-holding-basket-detergents-gesturing-thumb-up-portrait-smiling-camera-happy-243257363.jpg", "Eco-friendly cleaning services."),

)
