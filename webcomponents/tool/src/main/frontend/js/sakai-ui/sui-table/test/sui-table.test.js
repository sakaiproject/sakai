import { html, fixture, expect } from '@open-wc/testing';
import '../sui-table.js';

/* eslint-disable */
describe("SuiTable", () => {
  it("has a columns array", async () => {
/* eslint-enable */
    const el = await fixture(html` <sui-table></sui-table> `);

    expect(el.columns).isArray();
  });

  //   it('increases the counter on button click', async () => {
  //     const el = await fixture(html`
  //       <my-element></my-element>
  //     `);
  //     el.shadowRoot.querySelector('button').click();

  //     expect(el.counter).to.equal(6);
  //   });

  //   it('can override the title via attribute', async () => {
  //     const el = await fixture(html`
  //       <my-element title="attribute title"></my-element>
  //     `);

  //     expect(el.title).to.equal('attribute title');
  //   });

  //   it('passes the a11y audit', async () => {
  //     const el = await fixture(html`
  //       <my-element></my-element>
  //     `);

  //     await expect(el).shadowDom.to.be.accessible();
  //   });
});
