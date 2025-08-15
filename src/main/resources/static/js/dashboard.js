document.addEventListener("DOMContentLoaded", function () {
    // Top Selling Items Chart
    const topSellingCtx = document.getElementById("topSellingChart").getContext("2d");
    new Chart(topSellingCtx, {
        type: "bar",
        data: {
            labels: topItemsLabels,
            datasets: [{
                label: "Units Sold",
                data: topItemsData,
                backgroundColor: "rgba(54, 162, 235, 0.5)",
                borderColor: "rgba(54, 162, 235, 1)",
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });

    // Sales Trend Chart
    const salesTrendCtx = document.getElementById("salesTrendChart").getContext("2d");
    new Chart(salesTrendCtx, {
        type: "line",
        data: {
            labels: salesTrendLabels,
            datasets: [{
                label: "Daily Sales",
                data: salesTrendData,
                fill: false,
                borderColor: "rgba(255, 99, 132, 1)",
                tension: 0.1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
});

function openLowStockModal() {
    const modal = new bootstrap.Modal(document.getElementById('lowStockModal'));
    modal.show();
}

function openItemsReportModal() {
    const modal = new bootstrap.Modal(document.getElementById('allStockModal'));
    modal.show();
}

function downloadAllStockExcel() {
    window.location.href = '/all-stock.xlsx';
}

