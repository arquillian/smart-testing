require 'asciidoctor/extensions' unless RUBY_ENGINE == 'opal'

include ::Asciidoctor

# A block macro that embeds a Constant into the output document
#
# Usage
#
#   const:src/main/java/org/superbiz/Color.java[name="DEFAULT_COLOR"]
#   public static final String DEFAULT_COLOR = "blue";
#
#   const:src/main/java/org/superbiz/Color.java[tag="DEFAULT_COLOR"]
#   // const:DEFAULT_COLOR[]
#   public static final STRING MY_DEFAULT_COLOR = "blue";
#
class ConstBlockMacro < Extensions::InlineMacroProcessor
  use_dsl
  named :const

  def process parent, target, attrs

    data_path = parent.normalize_asset_path(target, 'target')
    const_value = []

    if attrs.has_key? 'name'
      const_name = attrs['name']

      File.open(data_path).each do |line|
        if (line =~ /\s#{const_name}(\s|=)/)
          # Gets content between double quotes
          const_value = line.scan(/"([^"]*)"/)
          break;
        end
      end
    else
      if attrs.has_key? 'tag'
        const_tag = %(const::#{attrs['tag']})
        found_tag = false
        File.open(data_path).each do |line|
          if found_tag
            # Gets content between double quotes
            const_value = line.scan(/"([^"]*)"/)
            break;
          end
          if (line =~ /\s#{const_tag}\s/)
            found_tag = true
          end
        end
      end
    end
    if const_value.nil? or const_value[0].nil? or const_value[0][0].nil?
      missing_attr = (attrs.has_key? 'tag') ? attrs['tag'] : attrs['name']
      raise "Failed reading '#{data_path}'. Check if constant '#{missing_attr}' exists"
    end
    return const_value[0][0].chomp
  end
end
