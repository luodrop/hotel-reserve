// Hotel edit - delegated click handler
$(document).on("click", ".edit-hotel-btn", function() {
    editHotel(
        $(this).data("id"),
        $(this).data("name") || "",
        $(this).data("address") || "",
        $(this).data("city") || "",
        $(this).data("description") || "",
        $(this).data("rating") || "",
        $(this).data("images") || ""
    );
});

// Room edit - delegated click handler
$(document).on("click", ".edit-room-btn", function() {
    editRoom(
        $(this).data("id"),
        $(this).data("typename") || "",
        $(this).data("price") || "",
        $(this).data("capacity") || "",
        $(this).data("description") || "",
        $(this).data("image") || ""
    );
});

$(document).ready(function() {
    $(".book-btn").on("click", function() {
        var roomId = $(this).data("room-id");
        var roomName = $(this).data("room-name");
        var roomPrice = $(this).data("room-price");
        var roomCapacity = $(this).data("room-capacity");
        var roomImage = $(this).data("room-image");

        $("#modalRoomId").val(roomId);
        $("#modalRoomName").text(roomName);
        $("#modalRoomPrice").text(roomPrice);
        $("#modalRoomCapacity").text("可住 " + roomCapacity + " 人");

        if (roomImage) {
            $("#modalRoomImage").attr("src", roomImage);
        }

        $("#summaryRoomName").text(roomName);
        $("#summaryPricePerNight").text(roomPrice);
        calculateTotal();
    });

    $("#checkIn, #checkOut").on("change", function() {
        calculateTotal();
    });

    var today = new Date().toISOString().split("T")[0];
    $("#checkIn").attr("min", today);

    $("#checkIn").on("change", function() {
        $("#checkOut").val("");
        $("#checkOut").attr("min", $(this).val());
    });
});

function calculateTotal() {
    var checkIn = $("#checkIn").val();
    var checkOut = $("#checkOut").val();
    var pricePerNight = parseFloat($("#modalRoomPrice").text()) || 0;

    if (checkIn && checkOut) {
        var start = new Date(checkIn);
        var end = new Date(checkOut);
        var nights = Math.max(0, Math.floor((end - start) / (1000 * 60 * 60 * 24)));

        if (nights > 0) {
            $("#summaryNights").text(nights);
            $("#summaryTotal").text((nights * pricePerNight).toFixed(2));
        } else {
            $("#summaryNights").text("0");
            $("#summaryTotal").text("0");
        }
    }
}

function clearHotelForm() {
    $("#hotelModalTitle").text("添加酒店");
    $("#hotelId").val("");
    $("#hotelName").val("");
    $("#hotelAddress").val("");
    $("#hotelCity").val("");
    $("#hotelDescription").val("");
    $("#hotelRating").val("");
    $("#hotelImages").val("");
}

function editHotel(id, name, address, city, description, rating, images) {
    $("#hotelModalTitle").text("编辑酒店");
    $("#hotelId").val(id);
    $("#hotelName").val(name);
    $("#hotelAddress").val(address);
    $("#hotelCity").val(city);
    $("#hotelDescription").val(description);
    $("#hotelRating").val(rating);
    $("#hotelImages").val(images);
}

function clearRoomForm() {
    $("#roomModalTitle").text("添加房型");
    $("#roomId").val("");
    $("#roomTypeName").val("");
    $("#roomPrice").val("");
    $("#roomCapacity").val("");
    $("#roomDescription").val("");
    $("#roomImage").val("");
}

function editRoom(id, typeName, price, capacity, description, image) {
    $("#roomModalTitle").text("编辑房型");
    $("#roomId").val(id);
    $("#roomTypeName").val(typeName);
    $("#roomPrice").val(price);
    $("#roomCapacity").val(capacity);
    $("#roomDescription").val(description);
    $("#roomImage").val(image);
}
