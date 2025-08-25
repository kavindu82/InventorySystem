function prepareDelete(e, button) {
    // Stop the link default and any Bootstrap handlers
    if (e) {
        e.preventDefault();
        e.stopPropagation();
    }

    const id = button.getAttribute("data-id");
    const itemNo = button.closest("tr").cells[1].textContent.trim();

    fetch(`/item/check-invoice-usage/${itemNo}`)
        .then(res => res.json())
        .then(data => {
            if (data.inUse) {
                // Only show info modal
                document.getElementById("infoModalBody").textContent =
                    "❌ Cannot delete. This item is already used in Invoice Items.";
                new bootstrap.Modal(document.getElementById('infoModal')).show();
                return;
            }
            // Only show delete confirm modal
            const form = document.getElementById("deleteForm");
            form.action = "/item/delete/" + id;
            new bootstrap.Modal(document.getElementById('deleteModal')).show();
        })
        .catch(() => {
            // Fallback: avoid accidental delete if check fails
            document.getElementById("infoModalBody").textContent =
                "⚠️ Could not verify usage. Please try again.";
            new bootstrap.Modal(document.getElementById('infoModal')).show();
        });
}

function fillEditModal(button) {
    const id = button.getAttribute('data-id');
    const itemNo = button.getAttribute('data-itemno');
    const itemName = button.getAttribute('data-itemname');
    const itemType = button.getAttribute('data-itemtype');

    document.getElementById('editId').value = id;
    document.getElementById('editItemNo').value = itemNo;
    document.getElementById('editItemName').value = itemName;
    document.getElementById('editItemType').value = itemType;
}

// 🔍 Search by Item No
document.getElementById('searchInput').addEventListener('keyup', function () {
    const query = this.value.toLowerCase();
    const rows = document.querySelectorAll("#itemTable tbody tr");

    rows.forEach(row => {
        const cells = row.querySelectorAll("td");
        const match = Array.from(cells).some(td => td.textContent.toLowerCase().includes(query));
        row.style.display = match ? "" : "none";
    });
});