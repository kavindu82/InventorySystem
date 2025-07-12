function prepareDelete(button) {
    const id = button.getAttribute("data-id");
    const form = document.getElementById("deleteForm");
    form.action = "/item/delete/" + id;
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
    let filter = this.value.toLowerCase();
    let rows = document.querySelectorAll("#itemTable tbody tr");

    rows.forEach(row => {
        let itemNo = row.cells[1].textContent.toLowerCase();
        row.style.display = itemNo.includes(filter) ? '' : 'none';
    });
});