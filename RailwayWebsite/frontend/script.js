/* =========================
   SECTION NAVIGATION
========================= */

function showSection(sectionName) {

    const sections =
        document.querySelectorAll(".page-section");

    const home =
        document.getElementById("home");


    /* Hide all pages */

    sections.forEach(function(section) {

        section.style.display = "none";

    });


    /* Show home */

    if (sectionName === "home") {

        home.style.display = "flex";

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });

        return;
    }


    /* Hide home */

    home.style.display = "none";


    /* Show selected section */

    const selected =
        document.getElementById(sectionName);


    if (selected) {

        selected.style.display = "block";

        window.scrollTo({
            top: selected.offsetTop - 80,
            behavior: "smooth"
        });

    }

}


/* =========================
   INITIAL PAGE
========================= */

window.addEventListener("load", function () {
    showSection("home");
});

function showSection(sectionName) {

    const sections = document.querySelectorAll(".page-section");
    const home = document.getElementById("home");

    sections.forEach(function(section) {
        section.style.display = "none";
    });

    if (sectionName === "home") {

        home.style.display = "flex";

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });

        return;
    }

    home.style.display = "none";

    const selected = document.getElementById(sectionName);

    if (selected) {
        selected.style.display = "block";

        window.scrollTo({
            top: selected.offsetTop - 80,
            behavior: "smooth"
        });
    }
}


/* =========================
   BOOK TICKET
========================= */

const bookingForm =
    document.getElementById("bookingForm");


if (bookingForm) {

    bookingForm.addEventListener(
        "submit",
        async function(event) {

            event.preventDefault();


            const bookingResult =
                document.getElementById(
                    "bookingResult"
                );


            bookingResult.innerHTML = `

                <div class="loading-box">

                    <h3>⏳ Processing Booking...</h3>

                    <p>
                        Please wait...
                    </p>

                </div>

            `;


            const formData =
                new URLSearchParams();


            formData.append(
                "name",
                document.getElementById("name").value
            );


            formData.append(
                "age",
                document.getElementById("age").value
            );


            formData.append(
                "gender",
                document.getElementById("gender").value
            );


            formData.append(
                "trainNumber",
                document.getElementById("trainNumber").value
            );


            formData.append(
                "trainName",
                document.getElementById("trainName").value
            );


            formData.append(
                "source",
                document.getElementById("source").value
            );


            formData.append(
                "destination",
                document.getElementById("destination").value
            );


            formData.append(
                "date",
                document.getElementById("date").value
            );


            formData.append(
                "className",
                document.getElementById("className").value
            );


            formData.append(
                "preference",
                document.getElementById("preference").value
            );


            try {

                const response =
                    await fetch(
                        "/api/book",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/x-www-form-urlencoded"
                            },

                            body: formData
                        }
                    );


                const result =
                    await response.text();


                bookingResult.innerHTML = `

                    <div class="confirmation-box">

                        <h2>
                            ✅ Booking Confirmed!
                        </h2>

                        <p>
                            Your railway ticket has
                            been successfully booked.
                        </p>

                        <div class="confirmation-details">

                            <pre>
${escapeHTML(result)}
                            </pre>

                        </div>

                        <p class="success-message">

                            🎫 Please keep your
                            PNR number safe.

                        </p>

                    </div>

                `;


                bookingResult.scrollIntoView({
                    behavior: "smooth",
                    block: "center"
                });


            } catch (error) {

                console.error(error);


                bookingResult.innerHTML = `

                    <div class="error-box">

                        <h2>
                            ❌ Booking Failed
                        </h2>

                        <p>
                            Could not connect to
                            Java backend.
                        </p>

                        <p>
                            Make sure
                            RailwayBackend.java
                            is running.
                        </p>

                    </div>

                `;

            }

        }
    );

}


/* =========================
   SEARCH PNR
========================= */

async function searchPNR() {

    const pnrInput =
        document.getElementById("pnrInput");


    const searchResult =
        document.getElementById("searchResult");


    const pnr =
        pnrInput.value.trim();


    if (pnr === "") {

        searchResult.innerHTML = `

            <div class="error-box">

                Please enter a PNR number.

            </div>

        `;

        return;

    }


    searchResult.innerHTML = `

        <div class="loading-box">

            <h3>⏳ Searching...</h3>

            <p>
                Please wait.
            </p>

        </div>

    `;


    try {

        const response =
            await fetch(
                "/api/search?pnr="
                + encodeURIComponent(pnr)
            );


        const result =
            await response.text();


        searchResult.innerHTML = `

            <div class="result-box">

                <h3>
                    🔎 Search Result
                </h3>

                <pre>
${escapeHTML(result)}
                </pre>

            </div>

        `;


    } catch (error) {

        console.error(error);


        searchResult.innerHTML = `

            <div class="error-box">

                <h3>
                    ❌ Search Failed
                </h3>

                <p>
                    Could not connect to
                    Java backend.
                </p>

            </div>

        `;

    }

}


/* =========================
   RESERVED TICKETS
========================= */

async function loadReservations() {

    const reservationList =
        document.getElementById(
            "reservationList"
        );


    reservationList.innerHTML = `

        <div class="loading-box">

            <h3>
                ⏳ Loading Reservations...
            </h3>

            <p>
                Please wait...
            </p>

        </div>

    `;


    try {

        const response =
            await fetch(
                "/api/reservations"
            );


        const result =
            await response.text();


        reservationList.innerHTML = `

            <div class="result-box">

                <pre>
${escapeHTML(result)}
                </pre>

            </div>

        `;


    } catch (error) {

        console.error(error);


        reservationList.innerHTML = `

            <div class="error-box">

                <h3>
                    ❌ Failed
                </h3>

                <p>
                    Could not load reservations.
                </p>

            </div>

        `;

    }

}


/* =========================
   TRAIN SCHEDULE
========================= */

async function loadSchedule() {

    const scheduleBody =
        document.getElementById(
            "scheduleBody"
        );


    scheduleBody.innerHTML = `

        <div class="loading-box">

            ⏳ Loading Train Schedule...

        </div>

    `;


    try {

        const response =
            await fetch(
                "/api/schedule"
            );


        const result =
            await response.text();


        scheduleBody.innerHTML = `

            <div class="result-box">

                <pre class="schedule-output">
${escapeHTML(result)}
                </pre>

            </div>

        `;


    } catch (error) {

        console.error(error);


        scheduleBody.innerHTML = `

            <div class="error-box">

                ❌ Could not load schedule.

            </div>

        `;

    }

}


/* =========================
   HTML SECURITY
========================= */

function escapeHTML(text) {

    return text

        .replace(/&/g, "&amp;")

        .replace(/</g, "&lt;")

        .replace(/>/g, "&gt;")

        .replace(/"/g, "&quot;")

        .replace(/'/g, "&#039;");

}