const form = document.querySelector('.anf-form');
const success = document.querySelector('.anf-results');
const tbody = success.querySelector('tbody');
const failed = document.querySelector('.anf-empty');

function found(dat) {
    success.style.display = 'block';
    tbody.innerHTML = '';
    [...dat].forEach(page => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${page.title}</td>
            <td>${page.description}</td>
            <td>
                <img style="display: flex;" src="${page.image}"/>
            </td>
            <td>${page.lastModified}</td>
        `
        tbody.appendChild(row);
    })
}

function search(e) {
    e.preventDefault();
    success.style.display = 'none';
    failed.style.display = 'none';

    const fulltext = this.querySelector('[name=fulltext]').value;

    fetch(`/bin/anfsearch?fulltext=${fulltext}`)
        .then(response => {
            if (!response.ok)
                throw new Error(response.statusText);
            return response.json();
        }).then(found)
        .catch(e => {
            failed.style.display = 'block';
        }
    );
}

form.addEventListener('submit', search);