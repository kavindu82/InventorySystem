function viewSaleDetails(button) {
    const saleId = button.getAttribute("data-id");

    fetch(`/sales/api/${saleId}`)
        .then(response => response.json())
        .then(sale => {
            // Fill modal fields (readonly)
            document.getElementById("viewClientName").value = sale.clientName;
            document.getElementById("viewClientContact").value = sale.clientContact;
            document.getElementById("viewSaleDate").value = sale.saleDate;
            document.getElementById("viewPaymentStatus").value = sale.paymentStatus;

            // Populate items table
            const tbody = document.getElementById("viewSaleItemsTableBody");
            tbody.innerHTML = ""; // Clear old rows
            sale.items.forEach(item => {
                const row = `
                    <tr>
                        <td>${item.itemNo}</td>
                        <td>${item.itemName}</td>
                        <td>${item.quantity}</td>
                        <td>${item.sellingPrice}</td>
                        <td>${item.amount}</td>
                    </tr>
                `;
                tbody.innerHTML += row;
            });

            document.getElementById("viewTotalAmount").value = sale.totalAmount;
            document.getElementById("viewDiscountPercentage").value = sale.discountPercentage;
            document.getElementById("viewFinalAmount").value = sale.finalAmount;

            // Show modal
            new bootstrap.Modal(document.getElementById('viewSaleModal')).show();
        })
        .catch(error => {
            console.error("Error loading sale details:", error);
        });
}
