function Footer() {
  return (
    <footer className="footer" id="about">
      <div className="footer__content">
        <span>© {new Date().getFullYear()} HomeMate</span>
        <span>Need help? Email support@homemate.app</span>
      </div>
    </footer>
  );
}

export default Footer;

